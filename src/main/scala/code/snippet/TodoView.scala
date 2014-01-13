package code
package snippet

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._

import code.model.Todo
import net.liftweb.mapper._
import net.liftweb.http._
import S._
import SHtml._
import net.liftweb.http.js.{JsCmd, JsCmds}

//Session Variable. Do you want to know well about variables and it's scopes? see below link.
//https://www.assembla.com/spaces/liftweb/wiki/Managing_State
object QueryNotDone extends SessionVar(false) 

class TodoView {
  
  def list(html: NodeSeq): NodeSeq = {

	  def renderRow(): NodeSeq = {
	    def reDraw() = JsCmds.Replace("all_todos",renderRow())
	    var render = "#all_todos * " #> ((n: NodeSeq) => doList(reDraw)(n))
	    render(html)
	  }
	  
	  renderRow()
	  
  }

    
  def add(form: NodeSeq): NodeSeq = {
   var title = ""
      
    def addTodo() = {
	    // if you get title from request, you write like below
	    // title = S.param("title").map(_.toString) openOr "" // get from request value

	    var todo = new Todo(title)
	    
	    todo.validate match{
	      case Nil => todo.save(); S.notice("Added " + todo.title)
	      case x => S.error(x); S.mapSnippet("TodoView.add", doBind)
	    }
    }
    
    def doBind(form: NodeSeq): NodeSeq = {
        var sel = 
          "name=title" #> SHtml.onSubmit(title = _) &
          "type=submit" #> SHtml.onSubmitUnit(addTodo) ;

        return sel(form)
        
    }
    
    doBind(form)
    
  }
  
  def deleteAll(form: NodeSeq): NodeSeq = {
    
    def deleteTodos() = {
      Todo.deleteDoned
      S.notice("Delete doned Todos")
    }
    

    var sel = "type=submit" #> SHtml.onSubmitUnit(deleteTodos);
    return sel(form)    
  }
  
  private def titleView(td: Todo, reDraw:() => JsCmd) = {
 	swappable(<span>{td.title.get}</span>,
              <span>{ajaxText(td.title.get,
                              v => {td.title(v).save; reDraw()})}
              </span>)
  }
  
  private def doList(reDraw: () => JsCmd)(html: NodeSeq): NodeSeq = {
    //find all todos.
    //Do you want to know more about query? see below link
    //https://www.assembla.com/wiki/show/liftweb/Mapper#querying_the_database
    var todos:List[Todo] = Todo.findAll(OrderBy(Todo.done,Ascending),OrderBy(Todo.dateBegin,Descending))
    todos.flatMap(t => 
      bind("todo",html,
        AttrBindParam("id",t.id.toString,"id"),
        "done" -> SHtml.ajaxCheckbox(t.done.get, v => { t.toggleDone().save();reDraw();
         }) ,
	    "title" -> titleView(t,reDraw),
	    "minutes" -> t.minutes
	  )
    )    
  }
  
  //Do you want to know well about ajaxProcessing? see below link
  //http://cookbook.liftweb.net/#AjaxFormProcessing
  //1.bind ajaxSubmit(ajaxInvoke) to dom event.
  //2.set callback for render(usually use SetHtml or Replace).
  
}

