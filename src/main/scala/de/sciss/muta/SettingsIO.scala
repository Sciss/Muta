package de.sciss.muta

import java.io.{FileInputStream, FileOutputStream, OutputStreamWriter, File}
import play.api.libs.json.{Reads, Writes, Json}

object SettingsIO {
  def write[S](settings: S, file: File)(implicit writes: Writes[S]): Unit = {
    val json  = Json.toJson(settings) // (Formats.settings)
    val w     = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")
    w.write(Json.prettyPrint(json))
    w.flush()
    w.close()
  }

  def read[S](file: File)(implicit reads: Reads[S]): S = {
    val fis   = new FileInputStream(file)
    val sz    = fis.available()
    val arr   = new Array[Byte](sz)
    fis.read(arr)
    fis.close()
    val str   = new String(arr, "UTF-8")
    val json  = Json.parse(str)
    val res   = Json.fromJson[S](json) // (Formats.settings)
    res.getOrElse(sys.error("JSON decoding failed"))
  }
}