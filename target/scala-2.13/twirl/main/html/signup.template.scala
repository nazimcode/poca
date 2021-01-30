
package html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml

object signup extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*2.1*/("""
"""),format.raw/*3.1*/("""<!doctype html>
<html>
    <head></head>

    <h1>Signup</h1>

    <p>Here you can signup to our marvelous web application.</p>

    <form action=/register method="post">
        <label for="username">username:</label><br>
        <input type="text" name="username"></input><br>
        <input type="submit">
    </form>
</html>
"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2021-01-30T15:18:07.788057
                  SOURCE: /home/nazim/M2diderot/poca/poca-2020/src/main/twirl/signup.scala.html
                  HASH: 24a603c8d59ce5c4957b9a683889c2dca44dfcfa
                  MATRIX: 554->1|650->4|677->5
                  LINES: 14->1|19->2|20->3
                  -- GENERATED --
              */
          