package dev.challenge.minify.cache.aerospike

trait KeyFormat[K] {
  def write(key: K): String
}

import com.aerospike.client.{Bin, Record}

trait ValueFormat[V] {

  implicit class BinStringOps(value: String) {
    def asBin(name: String): Bin = {
      new Bin(name, value)
    }
  }

  def write(value: V): Seq[Bin]

  def read(record: Record): V

}

object SimpleFormats {

  implicit val stringKeyFormat: KeyFormat[String] = identity[String]

  implicit val stringValueFormat: ValueFormat[String] = new ValueFormat[String] {
    override def write(value: String): Seq[Bin] = Seq(value.asBin("value"))

    override def read(record: Record): String = record.getString("value")
  }

}
