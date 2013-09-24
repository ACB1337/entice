/**
 * For copyright information see the LICENSE document.
 */

package entice.protocol.utils

import entice.protocol._
import akka.io._
import akka.event.LoggingAdapter
import scala.pickling._, json._
import java.nio.ByteOrder


/**
 * Defines the network de/serialization stack
 */
object PipelineFactory {

    def getWithLog(log: LoggingAdapter) = {
        TcpPipelineHandler.withLogger(log,

        new MessageStage >>
        new StringByteStringAdapter("utf-8") >>
        new LengthFieldFrame(
            maxSize = 1024,
            headerSize = 2,
            lengthIncludesHeader = false) >>
        new TcpReadWriteAdapter)
    }
}


/**
 * De/serializes a json object to/from a message case-class
 */
class MessageStage extends SymmetricPipelineStage[PipelineContext, Message, String] {
 
    override def apply(ctx: PipelineContext) = new SymmetricPipePair[Message, String] {

        override val commandPipeline = { msg: Message =>
            ctx.singleCommand(msg.pickle.value)
        }
 
        override val eventPipeline = { js: String =>
            ctx.singleEvent(toJSONPickle(js).unpickle[Message])
        }
    }
}