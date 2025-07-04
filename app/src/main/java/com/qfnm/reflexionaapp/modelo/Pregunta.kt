package com.qfnm.reflexionaapp.modelo

import java.io.Serializable

data class Pregunta(
    val texto: String,
    val categoria: String
): Serializable