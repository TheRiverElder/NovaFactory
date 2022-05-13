package io.theriverelder.novafactory.builtin.server

import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.io.json.JsonObject

class CommandHandler(
    val head: String,
    val handle: (String, JsonObject, WebClient) -> ActionResult<String, *>,
)