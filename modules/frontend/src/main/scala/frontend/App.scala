package frontend

import org.scalajs.dom.document
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.raw.Props

import scala.scalajs.js
import scala.scalajs.js.JSApp

object App extends JSApp {

  final case class TodoItem(id: Long, name: String)

  final case class TodoListProps(items: List[TodoItem], onRemove: Long => Callback)
  val TodoList = ScalaComponent
    .builder[TodoListProps]("TodoList")
    .render_P { props =>
      def createItem(item: TodoItem) = <.li(item.name, <.button(" [x]", item.id, ^.onClick --> props.onRemove(item.id)) )
      <.ul(props.items.map(createItem): _*)
    }
    .build

  //

  final case class State(items: List[TodoItem], text: String)
  class Backend($ : BackendScope[Unit, State]) {
    var nextId = 1

    def onChange(e: ReactEventFromInput) = {
      val newValue = e.target.value
      $.modState(_.copy(text = newValue))
    }

    def handleSubmit(e: ReactEventFromInput) =
      e.preventDefaultCB >>
      $.modState(s => State(s.items :+ TodoItem(nextId, s.text), "")) >>
      Callback { nextId += 1 }

    def onRemove(id: Long) = $.modState(s => s.copy(items = s.items.filter(x => x.id != id)))

    def render(state: State) =
      <.div(
        <.h3("TODO LIST"),
        TodoList(TodoListProps(state.items, onRemove)),
        <.form(^.onSubmit ==> handleSubmit,
               <.input(^.onChange ==> onChange, ^.value := state.text),
               <.button("Add #", state.items.length + 1))
      )
  }

  val TodoApp = ScalaComponent
    .builder[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .renderBackend[Backend]
    .build

  def main(): Unit = {
    val appDiv = document.getElementById("app")
    TodoApp().renderIntoDOM(appDiv)
  }
}
