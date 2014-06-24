/*
 *  SettingsIO.scala
 *  (Muta)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.muta

import java.io.{FileInputStream, FileOutputStream, OutputStreamWriter, File}
import play.api.libs.json.{JsError, JsSuccess, Reads, Writes, Json}
import scala.util.{Success, Failure, Try}

object SettingsIO {
  def write[S](settings: S, file: File)(implicit writes: Writes[S]): Try[Unit] = Try {
    val json  = Json.toJson(settings) // (Formats.settings)
    val w     = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")
    try {
      w.write(Json.prettyPrint(json))
      w.flush()
    } finally {
      w.close()
    }
  }

  def read[S](file: File)(implicit reads: Reads[S]): Try[S] = {
    val fis   = new FileInputStream(file)
    val sz    = fis.available()
    val arr   = new Array[Byte](sz)
    fis.read(arr)
    fis.close()
    val str   = new String(arr, "UTF-8")
    val json  = Json.parse(str)
    val res   = Json.fromJson[S](json) // (Formats.settings)
    // res.getOrElse(sys.error("JSON decoding failed"))
    res match {
      case JsSuccess(s, _)  => Success(s)
      case JsError(e)       => Failure(new Exception(e.mkString(", ")))
    }
  }
}