package io.theriverelder.novafactory.data

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.CellPrototype
import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.event.EventDispatcher
import io.theriverelder.novafactory.util.io.json.*

class NovaFactory : Tickable, ToJson, Persistent {

    var account: Double = 0.0
    var buttery: Double = 0.0
    var electricityGeneratingSpeed: Double = 0.0

    fun toInfoJson(): JsonSerializable {
        return JsonObject(
            "buttery" to JsonNumber(buttery),
            "account" to JsonNumber(account),
            "shop" to JsonArray(shop.map { it.toJson() }),
            "storage" to JsonArray(storage.map { it.toJson() }),
            "levelInfo" to JsonString(Game.level?.getInfo(this) ?: ""),
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
            "buttery" to JsonNumber(buttery),
            "account" to JsonNumber(account),
            "reactors" to JsonArray(reactors.map { it.toJson() }),
            "shop" to JsonArray(shop.map { it.toJson() }),
            "storage" to JsonArray(storage.map { it.toJson() }),
            "levelInfo" to JsonString(Game.level?.getInfo(this) ?: ""),
        )
    }

    val reactors: MutableList<Reactor> = ArrayList()

    fun addReactor(reactor: Reactor) {
        reactors.add(reactor)
    }

    override fun onTick() {
        onPreTickHandlers.emit(this)

        var lastGenElectricity = 0.0
        for (reactor in reactors) {
            onReactorPreTickHandlers.emit(reactor)
            reactor.tick()
            onReactorPostTickHandlers.emit(reactor)

            lastGenElectricity += reactor.electricityCache
            reactor.electricityCache = 0.0
        }
        buttery += lastGenElectricity
        electricityGeneratingSpeed = lastGenElectricity

        onPostTickHandlers.emit(this)
    }

    override fun read(json: JsonObject) {
        reactors.clear()
        reactors.addAll(json["reactors"].array
            .map { it.obj }
            .map { reactorJson -> Reactor(this).also { read(reactorJson) } })

        shop.clear()
        // TODO
//        shop.addAll(json["shop"].array
//            .map { it.obj }
//            .map { siJson -> S(this).also { read(siJson) } })

        storage.clear()
        storage.addAll(json["storage"].array
            .map { it.obj }
            .map { itemJson -> Game.REG_CELL[itemJson["id"].string].create().also { read(itemJson) } })
    }

    override fun write(): JsonObject {
        return JsonObject(
            "reactors" to JsonArray(reactors.map { it.write() }),
            "shop" to JsonArray(shop.map { it.write() }),
            "storage" to JsonArray(storage.map { it.write() }),
        )
    }

    fun output(amount: Double = buttery * 0.5): Double {
        buttery -= amount
        return amount
    }

    val shop: MutableList<CellPrototype> = ArrayList()
    val storage: MutableList<Cell> = ArrayList()

    fun buy(shopItemIndex: Int) {
        val si = getShopItem(shopItemIndex)
        if (account < si.price) throw Exception("账户余额不足(${account}/${si.price})")
        account -= si.price
        storage.add(si.create())
    }

    fun sell(storageItemIndex: Int) {
        val item = getStorageItem(storageItemIndex)
        storage.removeAt(storageItemIndex)
        account += 1
    }

    fun use(reactorIndex: Int, slotNumber: Int, storageItemIndex: Int) {
        val item = getStorageItem(storageItemIndex)
        val slot = getSlot(reactorIndex, slotNumber)

        if (slot.cell != null) throw Exception("编号为${slotNumber}的单元槽不是空的")

        slot.cell = item
    }

    fun pull(reactorIndex: Int, slotNumber: Int) {
        val slot = getSlot(reactorIndex, slotNumber)

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

    // 事件注册
    val onReactorPreTickHandlers: EventDispatcher<Reactor> = EventDispatcher()
    val onReactorPostTickHandlers: EventDispatcher<Reactor> = EventDispatcher()
    val onPreTickHandlers: EventDispatcher<NovaFactory> = EventDispatcher()
    val onPostTickHandlers: EventDispatcher<NovaFactory> = EventDispatcher()
}

fun NovaFactory.getShopItem(index: Int): CellPrototype {
    return shop.getOrNull(index) ?: throw Exception("未找到位置在[${index}]的商品")
}

fun NovaFactory.getStorageItem(index: Int): Cell {
    return storage.getOrNull(index) ?: throw Exception("未在仓库找到位置在[${index}]的物品")
}

fun NovaFactory.getReactor(index: Int): Reactor {
    return reactors.getOrNull(index) ?: throw Exception("未找到位置在[${index}]的反应堆")
}

fun NovaFactory.getSlot(reactorIndex: Int, slotNumber: Int): CellSlot {
    val reactor = getReactor(reactorIndex)
    return reactor.tryGetCellSlot(slotNumber) ?: throw Exception("未找到编号为[${slotNumber}]的物品槽")
}