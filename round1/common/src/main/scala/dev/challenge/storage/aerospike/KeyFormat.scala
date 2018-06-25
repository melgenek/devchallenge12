package dev.challenge.storage.aerospike

trait KeyFormat[K] {
  def write(key: K): String
}
