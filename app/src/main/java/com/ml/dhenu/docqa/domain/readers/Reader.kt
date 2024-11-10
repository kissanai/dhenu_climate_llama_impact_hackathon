package com.ml.dhenu.docqa.domain.readers

import java.io.InputStream

abstract class Reader {

    abstract fun readFromInputStream(inputStream: InputStream): String?
}
