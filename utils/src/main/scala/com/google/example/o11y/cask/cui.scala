package com.google.example.o11y.cask

import com.google.example.o11y.injectCuiNow
import _root_.cask.{Request, RawDecorator}

/** Cask wrapper that adds CUI to baggage for remaining processing */
class cui(val id: String) extends RawDecorator:
  def wrapFunction(ctx: Request, delegate: Delegate) =
    // Insert this CUI into baggage.
    val scope = injectCuiNow(id)
    try delegate(Map())
    finally scope.close()
