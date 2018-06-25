package dev.challenge.storage.aerospike

import com.aerospike.client.{Bin, Record}
import scala.collection.JavaConverters._

trait ValueFormat[V] {

  implicit class BinStringOps(value: String) {
    def asBin(name: String): Bin = {
      new Bin(name, value)
    }
  }

  implicit class BinLongOps(value: Long) {
    def asBin(name: String): Bin = {
      new Bin(name, value)
    }
  }

  implicit class BinListOps(value: List[String]) {
    def asBin(name: String): Bin = {
      new Bin(name, value.asJava)
    }
  }

  def write(value: V): Seq[Bin]

  def read(record: Record): V

}
