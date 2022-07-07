package io.theriverelder.novafactory.data

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.CellPrototype
import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.data.task.FactoryTask
import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.interfaces.findByUid
import io.theriverelder.novafactory.interfaces.removeByUid
import io.theriverelder.novafactory.interfaces.tryFindByUid
import io.theriverelder.novafactory.util.event.EventDispatcher
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.math.UidGen
import io.theriverelder.novafactory.util.persistence.*

class NovaFactory : Tickable, ToJson, Persistent {

    var cellUidGen = UidGen()
    var reactorUidGen = UidGen()
    var shopItemUidGen = UidGen()

    var account: Double = 0.0
    var buttery: Double = 0.0
    var lastGenElectricity: Double = 0.0

    fun toInfoJson(): JsonSerializable {
        return JsonObject(
            "lastGenElectricity" to JsonNumber(lastGenElectricity),
            "buttery" to JsonNumber(buttery),
            "account" to JsonNumber(account),
            "shop" to JsonArray(shop.map { it.toJson() }),
            "storage" to JsonArray(storage.map { it.toJson() }),
            "reactors" to JsonArray(reactors.mapIndexed { index, r -> JsonObject(
                "index" to JsonNumber(index),
                "size" to JsonNumber(r.size),
                "running" to jsonBoolean(r.running),
                "temperature" to JsonNumber(r.temperature),
            ) }),
        )
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "lastGenElectricity" to JsonNumber(lastGenElectricity),
            "buttery" to JsonNumber(buttery),
            "account" to JsonNumber(account),
            "shop" to JsonArray(shop.map { it.toJson() }),
            "storage" to JsonArray(storage.map { it.toJson() }),
            "reactors" to JsonArray(reactors.mapIndexed { index, r -> JsonObject(
                "index" to JsonNumber(index),
                "size" to JsonNumber(r.size),
                "running" to jsonBoolean(r.running),
                "temperature" to JsonNumber(r.temperature),
            ) }),
        )
    }

    val reactors: MutableList<Reactor> = ArrayList()

    fun addReactor(reactor: Reactor) {
        reactors.add(reactor)
    }

    override fun onTick() {
        onPreTickHandlers.emit(this)

        executeTasks()

        var lastGenElectricity = 0.0
        for (reactor in reactors) {
            onReactorPreTickHandlers.emit(reactor)
            reactor.tick()
            onReactorPostTickHandlers.emit(reactor)

            lastGenElectricity += reactor.electricityCache
            reactor.electricityCache = 0.0
        }
        buttery += lastGenElectricity
        this.lastGenElectricity = lastGenElectricity

        onPostTickHandlers.emit(this)
    }

    override fun read(json: JsonObject) {
        cellUidGen = restoreUidGen(json["cellUidGen"].obj)
        reactorUidGen = restoreUidGen(json["reactorUidGen"].obj)
        shopItemUidGen = restoreUidGen(json["shopItemUidGen"].obj)

        buttery = json["buttery"].number.toDouble()
        account = json["account"].number.toDouble()
        lastGenElectricity = json["lastGenElectricity"].number.toDouble()

        reactors.clear()
        reactors.addAll(json["reactors"].array.map { restoreReactor(it.obj, this) })

        shop.clear()
        shop.addAll(json["shop"].array.map { restoreCellPrototype(it.obj) })

        storage.clear()
        storage.addAll(json["storage"].array.map { restoreCell(it.obj) })

        tasks.clear()
        tasks.addAll(json["tasks"].array.map { restoreTask(it.obj) })
    }

    override fun write(): JsonObject {
        return JsonObject(
            "cellUidGen" to cellUidGen.write(),
            "reactorUidGen" to reactorUidGen.write(),
            "shopItemUidGen" to shopItemUidGen.write(),
            "buttery" to JsonNumber(buttery),
            "account" to JsonNumber(account),
            "lastGenElectricity" to JsonNumber(lastGenElectricity),
            "shop" to JsonArray(shop.map(Persistent::write)),
            "storage" to JsonArray(storage.map(Persistent::write)),
            "reactors" to JsonArray(reactors.map(Persistent::write)),
            "tasks" to JsonArray(tasks.map(Persistent::write)),
        )
    }

    fun output(amount: Double = buttery * 0.5): Double {
        buttery -= amount
        return amount
    }

    val shop: MutableList<CellPrototype> = ArrayList()
    val storage: MutableList<Cell> = ArrayList()

    fun buy(shopItemUid: Int) {
        val si = getShopItem(shopItemUid)
        if (account < si.price) throw Exception("账户余额不足(${account}/${si.price})")
        account -= si.price
        storage.add(si.create())
    }

    fun sell(storageItemUid: Int) {
        val item = getStorageItem(storageItemUid)
        storage.removeByUid(storageItemUid)
        account += item.mass
    }

    fun use(reactorUid: Int, slotNumber: Int, storageItemUid: Int) {
        val item = getStorageItem(storageItemUid)
        val slot = getSlot(reactorUid, slotNumber)

        if (slot.cell != null) throw Exception("编号为${slotNumber}的单元槽不是空的")

        storage.removeByUid(storageItemUid)
        slot.cell = item
    }

    fun pull(reactorUid: Int, slotNumber: Int) {
        val slot = getSlot(reactorUid, slotNumber)

        val cell = slot.cell ?: throw Exception("编号为${slotNumber}的单元槽是空的")
        if (slot.depth > 0) throw Exception("编号为${slotNumber}的单元槽插入深度大于0")

        slot.cell = null
        storage.add(cell)
    }

    fun turn(newStatus: Boolean) {
        val action = if (newStatus) "开启" else "暂停"
        if (newStatus == Game.running) throw Exception("游戏已经${action}")
        Game.running = newStatus
    }

    val tasks: MutableList<FactoryTask> = ArrayList()

    fun executeTasks() {
        var i = 0
        while (tasks.size > i) {
            val task = tasks[i]
            if (task.finished) {
                tasks.removeAt(i)
                continue
            }
            task.tick()
            if (task.finished) {
                tasks.removeAt(i)
            } else {
                i++
            }
        }
    }

    fun addTask(task: FactoryTask) {
        tasks.add(task)
    }

    fun addTasks(newTasks: Iterable<FactoryTask>) {
        tasks.addAll(newTasks)
    }

    fun removeTask(task: FactoryTask) {
        tasks.remove(task)
    }

    // 事件注册
    val onReactorPreTickHandlers: EventDispatcher<Reactor> = EventDispatcher()
    val onReactorPostTickHandlers: EventDispatcher<Reactor> = EventDispatcher()
    val onPreTickHandlers: EventDispatcher<NovaFactory> = EventDispatcher()
    val onPostTickHandlers: EventDispatcher<NovaFactory> = EventDispatcher()
}

fun NovaFactory.getShopItem(uid: Int): CellPrototype {
    return shop.tryFindByUid(uid) ?: throw Exception("未找到位置在[${uid}]的商品")
}

fun NovaFactory.getStorageItem(uid: Int): Cell {
    return storage.tryFindByUid(uid) ?: throw Exception("未在仓库找到位置在[${uid}]的物品")
}

fun NovaFactory.getReactor(uid: Int): Reactor {
    return reactors.tryFindByUid(uid) ?: throw Exception("未找到位置在[${uid}]的反应堆")
}

fun NovaFactory.getSlot(reactorUid: Int, slotNumber: Int): CellSlot {
    val reactor = getReactor(reactorUid)
    return reactor.tryGetCellSlot(slotNumber) ?: throw Exception("未找到编号为[${slotNumber}]的物品槽")
}