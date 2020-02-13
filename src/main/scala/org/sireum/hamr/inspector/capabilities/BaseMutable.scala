package org.sireum.hamr.inspector.capabilities

import org.sireum

private[capabilities] trait BaseMutable extends org.sireum.Mutable {
  private var $isOwned: Boolean = false

  override def $owned: Boolean = $isOwned

  override def $owned_=(b: Boolean): this.type = {
    this.$isOwned = b
    this
  }

  override def $clone: this.type = this

  override def string: sireum.String = super.toString()
}
