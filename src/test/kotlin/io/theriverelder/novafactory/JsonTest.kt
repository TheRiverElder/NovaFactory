package io.theriverelder.novafactory

import io.theriverelder.novafactory.util.io.json.StringReader
import io.theriverelder.novafactory.util.io.json.convertToString
import io.theriverelder.novafactory.util.io.json.deserialize

fun main() {
    val source = """
        {
            "reactors": [
                {
                    "size": 25, 
                    "width": 5, 
                    "height": 5, 
                    "electricity": 1.9e+7, 
                    "slots": [
                        {
                            "number": 0, 
                            "x": 0, 
                            "y": 0, 
                            "cell": null
                        }, 
                        {
                            "number": 1, 
                            "x": 1, 
                            "y": 0, 
                            "cell": null
                        }, 
                        {
                            "number": 2, 
                            "x": 2, 
                            "y": 0, 
                            "cell": null
                        }, 
                        {
                            "number": 3, 
                            "x": 3, 
                            "y": 0, 
                            "cell": null
                        }, 
                        {
                            "number": 4, 
                            "x": 4, 
                            "y": 0, 
                            "cell": null
                        }, 
                        {
                            "number": 5, 
                            "x": 0, 
                            "y": 1, 
                            "cell": null
                        }, 
                        {
                            "number": 6, 
                            "x": 1, 
                            "y": 1, 
                            "cell": {
                                "id": "RadiationSourceCell", 
                                "heat": 6028498938.541726, 
                                "mass": 1000000, 
                                "heatCapacity": 1.2, 
                                "heatTransferFactor": 0.75
                            }
                        }, 
                        {
                            "number": 7, 
                            "x": 2, 
                            "y": 1, 
                            "cell": {
                                "id": "NuclearRodCell", 
                                "heat": 5879509552.62447, 
                                "mass": 955999.2863914439, 
                                "heatCapacity": 1.2, 
                                "heatTransferFactor": 0.75
                            }
                        }, 
                        {
                            "number": 8, 
                            "x": 3, 
                            "y": 1, 
                            "cell": {
                                "id": "NeutronMirrorCell", 
                                "heat": 5379476162.270089, 
                                "mass": 1000000, 
                                "heatCapacity": 1.4, 
                                "heatTransferFactor": 0.4
                            }
                        }, 
                        {
                            "number": 9, 
                            "x": 4, 
                            "y": 1, 
                            "cell": null
                        }, 
                        {
                            "number": 10, 
                            "x": 0, 
                            "y": 2, 
                            "cell": null
                        }, 
                        {
                            "number": 11, 
                            "x": 1, 
                            "y": 2, 
                            "cell": {
                                "id": "NuclearRodCell", 
                                "heat": 6114692517.637455, 
                                "mass": 955999.3057539376, 
                                "heatCapacity": 1.2, 
                                "heatTransferFactor": 0.75
                            }
                        }, 
                        {
                            "number": 12, 
                            "x": 2, 
                            "y": 2, 
                            "cell": {
                                "id": "GeneratorCell", 
                                "heat": 5916897706.946481, 
                                "mass": 1000000, 
                                "heatCapacity": 2, 
                                "heatTransferFactor": 0.9
                            }
                        }, 
                        {
                            "number": 13, 
                            "x": 3, 
                            "y": 2, 
                            "cell": {
                                "id": "NuclearRodCell", 
                                "heat": 5864838616.854062, 
                                "mass": 955999.1943317457, 
                                "heatCapacity": 1.2, 
                                "heatTransferFactor": 0.75
                            }
                        }, 
                        {
                            "number": 14, 
                            "x": 4, 
                            "y": 2, 
                            "cell": null
                        }, 
                        {
                            "number": 15, 
                            "x": 0, 
                            "y": 3, 
                            "cell": null
                        }, 
                        {
                            "number": 16, 
                            "x": 1, 
                            "y": 3, 
                            "cell": null
                        }, 
                        {
                            "number": 17, 
                            "x": 2, 
                            "y": 3, 
                            "cell": {
                                "id": "NuclearRodCell", 
                                "heat": 6110263898.611941, 
                                "mass": 955999.7409291414, 
                                "heatCapacity": 1.2, 
                                "heatTransferFactor": 0.75
                            }
                        }, 
                        {
                            "number": 18, 
                            "x": 3, 
                            "y": 3, 
                            "cell": {
                                "id": "RadiationSourceCell", 
                                "heat": 6034454645.76562, 
                                "mass": 1000000, 
                                "heatCapacity": 1.2, 
                                "heatTransferFactor": 0.75
                            }
                        }, 
                        {
                            "number": 19, 
                            "x": 4, 
                            "y": 3, 
                            "cell": null
                        }, 
                        {
                            "number": 20, 
                            "x": 0, 
                            "y": 4, 
                            "cell": null
                        }, 
                        {
                            "number": 21, 
                            "x": 1, 
                            "y": 4, 
                            "cell": null
                        }, 
                        {
                            "number": 22, 
                            "x": 2, 
                            "y": 4, 
                            "cell": null
                        }, 
                        {
                            "number": 23, 
                            "x": 3, 
                            "y": 4, 
                            "cell": null
                        }, 
                        {
                            "number": 24, 
                            "x": 4, 
                            "y": 4, 
                            "cell": null
                        }
                    ]
                }
            ], 
            "shop": [
                {
                    "tip": "NuclearRod"
                }, 
                {
                    "tip": "RadiationSource"
                }, 
                {
                    "tip": "Generator"
                }, 
                {
                    "tip": "NeutronMirror"
                }, 
                {
                    "tip": "CoolingBlock"
                }, 
                {
                    "tip": "ExtraRadiationSource"
                }
            ]
        }
    """.trimIndent()

//    val source = """
//        {
//            "a": 1234.5678,
//            "sss": {
//                "bb": 345
//            }
//        }
//    """.trimIndent()
    val input = StringReader(source)
    val json = deserialize(input)
    println(json?.convertToString())
}