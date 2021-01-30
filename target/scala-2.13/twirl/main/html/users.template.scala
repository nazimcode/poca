
package html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml

object users extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template1[Seq[poca.User],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(users: Seq[poca.User]):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*2.1*/("""
"""),format.raw/*3.1*/("""<!doctype html>
<html>
    <head></head>

    <h1>List of registred users</h1>

    <ul>
    """),_display_(/*10.6*/for(user <- users) yield /*10.24*/ {_display_(Seq[Any](format.raw/*10.26*/("""
        """),format.raw/*11.9*/("""<li>"""),_display_(/*11.14*/user/*11.18*/.username),format.raw/*11.27*/("""</li>
    """)))}),format.raw/*12.6*/("""
    """),format.raw/*13.5*/("""</ul>
        
</html>
"""))
      }
    }
  }

  def render(users:Seq[poca.User]): play.twirl.api.HtmlFormat.Appendable = apply(users)

  def f:((Seq[poca.User]) => play.twirl.api.HtmlFormat.Appendable) = (users) => apply(users)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2021-01-30T15:18:07.772792
                  SOURCE: /home/nazim/M2diderot/poca/poca-2020/src/main/twirl/users.scala.html
                  HASH: 213a19e4544f1e05689d1d3f42d5c4a0b9e77f3d
                  MATRIX: 568->1|685->25|712->26|832->120|866->138|906->140|942->149|974->154|987->158|1017->167|1058->178|1090->183
                  LINES: 14->1|19->2|20->3|27->10|27->10|27->10|28->11|28->11|28->11|28->11|29->12|30->13
                  -- GENERATED --
              */
          