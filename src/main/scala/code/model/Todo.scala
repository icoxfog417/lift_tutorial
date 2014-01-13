package code.model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date

//more detail , see wiki
//https://www.assembla.com/wiki/show/liftweb/Mapper

object Todo extends Todo with LongKeyedMetaMapper[Todo]{
  override def dbTableName = "todos" // define the DB table name

  // add static method (like findById etc ...)
  def deleteDoned(): Boolean = {
    val result: Boolean = Todo.bulkDelete_!!(By(Todo.done,true))
    return result
  }
}

class Todo extends LongKeyedMapper[Todo] with IdPK {
	 //additional constructor . create by title
	 def this(title:String) = {
	   this()
	   this.title(title)
	   this.done(false)
	   
	   var now = new Date()
	   this.dateBegin(now)
	   this.dateEnd(now)
	 }
	 
	 def getSingleton = Todo

	 //Fields 
	 object done extends MappedBoolean(this)

	 object title extends MappedString(this, 100) {
	   override def validations = 
	     valMaxLen(256, "message must be under 256 characters long ") _ :: 
	     valMinLen(1,"you haveto input") _ ::
	     super.validations
	 }
	 

	 object dateBegin extends MappedDateTime(this)
	 object dateEnd extends MappedDateTime(this)

	 //Business Logic. calculate minutes between begin and end of todo.
	 def minutes:String = {
	   if(this.done.get){
		   return (Math.round((dateEnd.get.getTime() - dateBegin.get.getTime()) /(1000 * 60))).toString; 
	   }else{
		   var now = new Date()
		   return (Math.round((now.getTime() - dateBegin.get.getTime()) /(1000 * 60))).toString; 	     
	   }
	 }
	 
	 //done this todo.
	 def toggleDone() :Todo = {
	   if(!this.done.get){ // if todo end,set date end.
		   this.done(true)
		   this.dateEnd(new Date())	     
	   }else{
		   this.done(false)
		   this.dateEnd(this.dateBegin.get)
	   }
	   
	   return this
	 }
	 	 
}

