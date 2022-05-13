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
        )
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "buttery" to JsonNumber(buttery),
            "account" to JsonNumber(account),
            "reactors" to JsonArray(reactors.map { it.toJson() }),
            "shop" to JsonArray(shop.map { it.toJson() }),
            "storage" to JsonArray(storage.map { it.toJson() }),
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

    fun buy(shopItemIndex: Int): ActionResult<String, Unit> {
        val si = shop.getOrNull(shopItemIndex) ?: return ActionResult(false, "未找到位置在[${shopItemIndex}]的商品", Unit)
        if (account < si.price) return ActionResult(false, "账户余额不足(${account}/${si.price})", Unit)
        account -= si.price
        storage.add(si.create())
        return ActionResult(true, "成功购买【${si.tip}】*1", Unit)
    }

    fun sell(itemIndex: Int): ActionResult<String, Cell?> {
        val item = storage.getOrNull(itemIndex) ?: return ActionResult(false, "未找到位置在[${itemIndex}]物品", null)
        account += 1
        return ActionResult(true, "成功在卖出【${item.javaClass.simpleName}】", item)
    }

    fun use(itemIndex: Int, reactorIndex: Int, slotIndex: Int): ActionResult<String, Unit> {
        val item = storage.getOrNull(itemIndex) //?: return ActionResult(false, "未找到位置在[${itemIndex}]的物品", Unit)
        val reactor = reactors.getOrNull(reactorIndex) ?: return ActionResult(false, "未找到位置在[${reactorIndex}]的反应堆", Unit)
        val slot = reactor.tryGetCellSlot(slotIndex) ?: return ActionResult(false, "未找到位置在[${slotIndex}]的物品槽", Unit)

        if (itemIndex >= 0 && itemIndex < storage.size) {
            storage.removeAt(itemIndex)
        }
        val useRes = use(item, slot)
        val prevCell = useRes.extra
        if (useRes.succeeded && prevCell != null) {
            storage.add(prevCell)
        }
        return ActionResult(true, "成功在${reactorIndex}号反应堆${slotIndex}号物品槽使用物品【${item?.javaClass?.simpleName ?: "null"}】", Unit)
    }

    fun use(item: Cell?, slot: CellSlot): ActionResult<String, Cell?> {
        val prevCell = slot.cell
        slot.cell = item
        return ActionResult(true, "成功将其替换为${if (item == null) "<Null>" else "【${item.javaClass.simpleName}】"}", prevCell)
    }

    // 事件注册
    val onReactorPreTickHandlers: EventDispatcher<Reactor> = EventDispatcher()
    val onReactorPostTickHandlers: EventDispatcher<Reactor> = EventDispatcher()
    val onPreTickHandlers: EventDispatcher<NovaFactory> = EventDispatcher()
    val onPostTickHandlers: EventDispatcher<NovaFactory> = EventDispatcher()
}