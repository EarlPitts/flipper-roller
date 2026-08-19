package flipper.web.view

import scalatags.Text.all.*
import org.http4s.scalatags.*
import scalatags.Text.TypedTag

import flipper.core.Flipper
import flipper.core.Flipper.*

val hxGet = attr("hx-get")
val hxPost = attr("hx-post")
val hxDelete = attr("hx-delete")
val hxTarget = attr("hx-target")
val hxSwap = attr("hx-swap")
val hxTrigger = attr("hx-trigger")

def template(t: String, content: TypedTag[String]) = html(
  head(
    // TODO figure out title
    // script(src := "..."),
    script(
      src := "https://cdn.jsdelivr.net/npm/htmx.org@2.0.10/dist/htmx.min.js",
      integrity := "sha384-H5SrcfygHmAuTDZphMHqBJLc3FhssKjG7w/CeCpFReSfwBWDTKpkzPP8c+cLsK+V",
      crossorigin := "anonymous"
    )
    // script(
    //   "alert('Hello World')"
    // )
  ),
  body(content)
)

def mainView(flippers: List[Flipper]) = div(
  h1(id := "title", "Flipper Roller"),
  span(
    form(
      hxPost := "/",
      hxTarget := "#flippers",
      hxSwap := "outerHTML"
    )(
      input(`type` := "text", name := "name"),
      input(`type` := "date", name := "date"),
      button(`type` := "submit")("Add")
    ),
    button(
      hxDelete := "/",
      hxTarget := "#flippers",
      hxSwap := "outerHTML"
    )("Delete All")
  ),
  flippersView(flippers)
)

def flippersView(flippers: List[Flipper]) = div(id := "flippers")(
  flippers.map(flipperView)
)

def flipperView(flipper: Flipper) =
  def rowView(name: String, status: String) = span(
    name,
    " ",
    status,
    " ",
    button(
      hxDelete := s"/$name",
      hxTarget := "#flippers",
      hxSwap := "outerHTML"
    )("Delete")
  )
  div(
    flipper match {
      case Waiting(name, _)    => rowView(name.unName, "Waiting")
      case InProgress(name, _) => rowView(name.unName, "In Progress")
      case Done(name, _)       => rowView(name.unName, "Done")
    }
  )
