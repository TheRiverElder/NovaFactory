package io.theriverelder.novafactory

import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.goal.Level
import io.theriverelder.novafactory.entrance.Plugin
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.Creator
import io.theriverelder.novafactory.util.event.EventDispatcher
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.registry.Registry
import io.theriverelder.novafactory.util.time.DeltaTime
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset

object Game : ToJson, Persistent {

    val REG_CELL = Registry<String, Creator<String, Cell>> { it.key }

    val onPreTickHandlers = EventDispatcher<Game>()
    val onPostTickHandlers = EventDispatcher<Game>()

    // 当前时刻距离上一时刻的变化量
//    var dt: DeltaTime = DeltaTime(0, 0)
    var time: Long = 0L
    // 具体游戏
    var factory: NovaFactory = NovaFactory()
    // 游戏是否继续
    var running: Boolean = false
    // 当前关卡的需求
    var level: Level? = null

    fun tick() {
        onPreTickHandlers.emit(this)
        if (running) {
            time++
            factory.tick()

            val goal = level
            if (goal != null) {
                val pack = factory.output()
                goal.input(factory, pack)
            }
        }
        onPostTickHandlers.emit(this)
    }

    /**
     * 对游戏进行初始化，改动游戏设置
     * 仅在游戏启动时运行一次
     */
    fun use(vararg plugins: Plugin) {
        for (plugin in plugins) {
            plugin.setup()
        }
    }

    /**
     * 开始新的一个关卡，并进行初始化设置
     * 每关开始时都运行一次
     */
    fun start(level: Level) {
        this.level = level
        level.setup(factory)
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "factory" to factory.toJson(),
            "time" to JsonNumber(time),
            "running" to JsonBoolean(running),
            "level" to (level?.toJson() ?: JSON_NULL),
            "levelInfo" to JsonString(level?.getInfo(factory) ?: ""),
        )
    }

    override fun read(json: JsonObject) {
        time = json["time"].number.toLong()
        factory.read(json["factory"].obj)
    }

    override fun write(): JsonObject {
        return JsonObject(
            "time" to JsonNumber(time),
            "factory" to factory.write(),
//            "level" to (level?.write() ?: JSON_NULL),
        )
    }

    fun save(path: String = "./save.json"): ActionResult<String, Exception?> {
        try {
            val output = StringBuilder()
            write().serialize(output)
            val writer = FileWriter(File(path))
            writer.write(output.toString())
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            return ActionResult(false, "保存失败：${path}", e)
        }
        return ActionResult(true, "保存成功：${path}", null)
    }

    fun load(path: String): ActionResult<String, IOException?> {
        try {
            val input = File(path).readText(Charsets.UTF_8)
            val json = deserialize(StringReader(input))
            if (json != null) {
                read(json.obj)
            }
        } catch (e: Exception) {
            return ActionResult(true, "载入成功：${path}", null)
        }
        return ActionResult(true, "载入成功：${path}", null)
    }

}