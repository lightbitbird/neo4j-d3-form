package controllers

import java.util.ArrayList

import scala.collection.JavaConverters._
import scala.collection.mutable.Buffer

import javax.inject.Inject
import neo4j.models.FriendRelation
import neo4j.models.Person
import neo4j.repositories.FriendRelationRepository
import neo4j.repositories.PersonRepository
import neo4j.services.Neo4jD3Service
import neo4j.services.Neo4jServiceProviderImpl
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.mvc.Flash

case class CaseFriendList(list: List[CaseFriend])
case class CaseFriend(checked: Boolean, id: Long, relationId:Long, name:String, stars: Int, comment: String)

class FriendRelationController @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

    val friendForm: Form[CaseFriendList] = Form (
        mapping(
            "list" -> list[CaseFriend](
                mapping(
                    "checked" -> boolean,
                    "id" -> longNumber,
                    "relationId" -> longNumber,
                    "name" -> text,
                    "stars" -> number,
                    "comment" -> text
                )(CaseFriend.apply)(CaseFriend.unapply)
            )
    )(CaseFriendList.apply)(CaseFriendList.unapply))

    def friendList(id: Long) = Neo4jTransactionAction { implicit request =>
        def neo4jD3Service: Neo4jD3Service = Neo4jServiceProviderImpl.get().neo4jD3Service;

        def allPersons: List[Person] = neo4jD3Service.getAllPersons().asScala.toList
        val repo: PersonRepository = neo4jD3Service.getPersonRepository()
        val dbPerson:Person = repo.findOne(id)
        val friends: List[Person] = new ArrayList[Person](dbPerson.personKnows).asScala.toList
        val friendRelations: List[FriendRelation] = new ArrayList[FriendRelation](dbPerson.friends).asScala.toList
        Ok(views.html.friend.friendList(getFriendForms(allPersons, friendRelations), dbPerson))
    }

    def getFriendForms(allPersons: List[Person], friendRelations:List[FriendRelation]): Form[CaseFriendList] = {

        val buffer = scala.collection.mutable.ListBuffer[CaseFriend]()
        allPersons.map { p => {
            val friendRelation = friendRelations.find { x => x.getOther().getId == p.getId }
                val elem = friendRelation match {
                    case Some(r) => CaseFriend(true, r.getOther().getId, r.getId, r.getOther().getName, r.getStars, r.getComment)
                    case _ => CaseFriend(false, p.getId, 0L, p.getName, 0, "")
                }
                buffer += elem
            }
        }
//        buffer.foreach { x => println("x => " + x) }
        friendForm.fill(CaseFriendList(buffer.toList))
    }

    def postEdit(id: Long) = Neo4jTransactionAction { implicit request =>
        friendForm.bindFromRequest().fold(formWithErrors => {
            def neo4jD3Service: Neo4jD3Service = Neo4jServiceProviderImpl.get().neo4jD3Service;
            val repo: PersonRepository = Neo4jServiceProviderImpl.get().neo4jD3Service.getPersonRepository()
            val dbPerson:Person = repo.findOne(id)

            Ok(views.html.friend.friendList(formWithErrors, dbPerson))
        }, 
        form => {
            def neo4jD3Service: Neo4jD3Service = Neo4jServiceProviderImpl.get().neo4jD3Service;
            val repo: PersonRepository = neo4jD3Service.getPersonRepository()

            val dbPerson:Person = repo.findOne(id)
            val friendRelations: List[FriendRelation] = new ArrayList[FriendRelation](dbPerson.friends).asScala.toList
            val relationRepo: FriendRelationRepository = neo4jD3Service.getFriendRelationRepository()
            form.list.foreach { x => {
                if (x.checked == true && id != x.id) {
                    val friend = repo.findOne(x.id)
                    //対象人物に関係するリレーションがあるか探す
                    val relation: Option[FriendRelation] = friendRelations.find { f => f.getId == x.relationId }
                    relation match {
                        case Some(r) => {
                            r.setOther(friend)
                            r.setStars(x.stars)
                            r.setComment(x.comment)
                            relationRepo.save(r)
                        }
                        case _ => {
                            neo4jD3Service.createRelation(dbPerson, friend, x.stars, x.comment)
                        }
                    }
               } else {
                   if (id != x.id) {
                        val relation: Option[FriendRelation] = friendRelations.find { f => f.getId == x.relationId }
                        if (relation != None) {
                            relation match {
                                case Some(r) => {
                                   relationRepo.delete(r)
                                }
                            }
                        }
                   }
               }

            } }

            Redirect(routes.FriendRelationController.friendList(id))
       })
    }
}
