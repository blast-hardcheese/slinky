package slinky.core

import slinky.core.facade.{React, ReactRaw, ReactElement}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import scala.scalajs.js.annotation.JSName

@js.native trait SyntheticEvent[TargetType, EventType] extends js.Object {
  val bubbles: Boolean = js.native
  val cancelable: Boolean = js.native
  val currentTarget: TargetType = js.native
  val defaultPrevented: Boolean = js.native
  val eventPhase: Int = js.native
  val isTrusted: Boolean = js.native
  val nativeEvent: EventType = js.native
  def preventDefault(): Unit = js.native
  def isDefaultPrevented(): Boolean = js.native
  def stopPropagation(): Unit = js.native
  def isPropagationStopped(): Boolean = js.native
  val target: TargetType = js.native
  val timeStamp: Int = js.native
  @JSName("type") val `type`: String = js.native
}

trait Tag extends Any {
  type tagType <: TagElement
  def apply(mods: TagMod[tagType]*): WithAttrs[tagType]
}

object Tag {
  implicit object ApplyReactElements
}

final class CustomTag(private val name: String) extends Tag {
  override type tagType = Nothing

  def apply(mods: TagMod[tagType]*): WithAttrs[tagType] = {
    new WithAttrs[tagType](js.Array(name, js.Dictionary.empty[js.Any])).apply(mods: _*)
  }
}

trait Attr {
  type attrType
  type supports[T <: Tag] = AttrPair[attrType] => AttrPair[T#tagType]
}

abstract class TagElement

final class CustomAttribute[T](private val name: String) {
  @inline def :=(v: T) = new AttrPair[Any](name, v.asInstanceOf[js.Any])
}

trait TagMod[-A] extends js.Object

@js.native trait ReactElementMod extends TagMod[Any]

object TagMod {
  @inline implicit def elemToTagMod[E](elem: E)(implicit ev: E => ReactElement): TagMod[Any] =
    ev(elem).asInstanceOf[ReactElementMod]

  @inline implicit def elemsToTagMods(elems: Seq[ReactElement]): Seq[TagMod[Any]] =
    elems.asInstanceOf[Seq[ReactElementMod]]
}

final class AttrPair[-A](@inline val name: String, @inline val value: js.Any) extends TagMod[A]

final class WithAttrs[A](@inline private val args: js.Array[js.Any]) extends AnyVal {
  @inline def apply(mods: TagMod[A]*): WithAttrs[A] = {
    mods.foreach { m =>
      m match {
        case a: AttrPair[_] =>
          args(1).asInstanceOf[js.Dictionary[js.Any]](a.name) = a.value
        case r =>
          args.push(r.asInstanceOf[ReactElementMod])
      }
    }

    this
  }
}

object WithAttrs {
  @inline implicit def shortCut(withAttrs: WithAttrs[_]): ReactElement = {
    ReactRaw.createElement
      .applyDynamic("apply")(ReactRaw, withAttrs.args).asInstanceOf[ReactElement]
  }
}
