package io.theriverelder.novafactory.builtin

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.builtin.cell.*
import io.theriverelder.novafactory.entrance.Plugin
import io.theriverelder.novafactory.util.Creator

class BuiltinPlugin : Plugin {

    override fun setup() {
        registerCells()
    }

    // 注册各种Cell
    private fun registerCells() {

        Game.REG_CELL.register(Creator(SlowingRodCell::class.simpleName!!) { SlowingRodCell() })
        Game.REG_CELL.register(Creator(CoolingBlockCell::class.simpleName!!) { CoolingBlockCell() })
        Game.REG_CELL.register(Creator(GeneratorCell::class.simpleName!!) { GeneratorCell() })
        Game.REG_CELL.register(Creator(NeutronMirrorCell::class.simpleName!!) { NeutronMirrorCell() })
        Game.REG_CELL.register(Creator(NuclearRodCell::class.simpleName!!) { NuclearRodCell() })
        Game.REG_CELL.register(Creator(RadiationSourceCell::class.simpleName!!) { RadiationSourceCell() })

    }
}