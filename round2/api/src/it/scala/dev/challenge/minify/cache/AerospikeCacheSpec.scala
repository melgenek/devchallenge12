package dev.challenge.minify.cache

import com.aerospike.client.Host
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import dev.challenge.minify.cache.aerospike.{Aerospike, AerospikeCache, AerospikeConfig}
import dev.challenge.minify.util.ItContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpecLike, Matchers}
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.duration._

class AerospikeCacheSpec extends FlatSpecLike with Matchers with ScalaFutures with ForAllTestContainer with ItContext {

  "cache" should "store a value" in new Wiring {
    val resFuture = for {
      _ <- cache.put("url1", "css1", ttl)
      res <- cache.find("url1")
    } yield res

    resFuture.futureValue should equal(Some("css1"))
  }

  it should "delete a value" in new Wiring {
    val resFuture = for {
      _ <- cache.put("url2", "css2", ttl)
      saved <- cache.find("url2")
      _ <- cache.delete("url2")
      deleted <- cache.find("url2")
    } yield (saved, deleted)

    val (saved, deleted) = resFuture.futureValue
    saved should equal(Some("css2"))
    deleted should equal(None)
  }

  it should "delete all values" in new Wiring {
    val resFuture = for {
      _ <- cache.put("url3", "css3", ttl)
      _ <- cache.put("url4", "css4", ttl)
      saved1 <- cache.find("url3")
      saved2 <- cache.find("url4")
      _ <- cache.deleteAll()
      deleted1 <- cache.find("url3")
      deleted2 <- cache.find("url4")
    } yield (saved1, saved2, deleted1, deleted2)

    val (saved1, saved2, deleted1, deleted2) = resFuture.futureValue
    saved1 should equal(Some("css3"))
    saved2 should equal(Some("css4"))
    deleted1 should equal(None)
    deleted2 should equal(None)
  }

  private trait Wiring {
    import dev.challenge.minify.cache.aerospike.SimpleFormats._

    val ttl: FiniteDuration = 100.seconds

    val aerospikeConfig = AerospikeConfig(host = new Host(
      container.container.getContainerIpAddress,
      container.container.getMappedPort(3000)
    ))

    val aerospikeClientWithEventLoops = Aerospike(aerospikeConfig)
    val cache = new AerospikeCache[String, String](aerospikeClientWithEventLoops, "test", "test_set")

  }

  override val container: GenericContainer = GenericContainer("aerospike/aerospike-server:3.15.1.4",
    exposedPorts = Seq(3000),
    waitStrategy = Wait.forLogMessage(".*in-progress.*\n?", 1)
  )

}
