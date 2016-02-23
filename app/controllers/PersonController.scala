package controllers

import org.apache.commons.lang.StringUtils

import javax.inject.Inject
import neo4j.models.Person
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

case class CasePerson(id: Long, name: String, email: String, password: String, nickname: String)

class PersonController @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

    val personForm: Form[CasePerson] = Form (
        mapping(
            "id" -> longNumber,
            "name" -> text,
            "email" -> email,
            "password" -> text,
            "nickname" -> text
    )(CasePerson.apply)(CasePerson.unapply))

    def index = Action {
        Ok(views.html.index("Hello Neo4j!!"))
    }

    def personList = Neo4jTransactionAction { implicit request =>
        def neo4jD3Service: Neo4jD3Service = Neo4jServiceProviderImpl.get().neo4jD3Service;

        if (neo4jD3Service.getNumberOfPersons() == 0) {
            neo4jD3Service.makeSomePersonsAndRelations()
        }

        def allPersons: java.util.List[Person] = neo4jD3Service.getAllPersons()
        val path = getPathFirstToLast(allPersons, neo4jD3Service)

        Ok(views.html.person.personList(allPersons, path))
//        Ok(views.html.index.render(allUsers, pathFromFirstToLast))
    }

    def getPathFirstToLast(persons: java.util.List[Person], neo4jD3Service: Neo4jD3Service) : java.util.List[Person] = {
        val first: Person = persons.get(0)
        val last: Person = persons.get(persons.size() - 1)
        val path: java.util.List[Person] = neo4jD3Service.getPersonPath(first, last)
        path
    }

    def create = Neo4jTransactionAction { implicit request => 
        Ok(views.html.person.personEdit(-1L, personForm, "create"))
    }

    def delete(id: Long) = Neo4jTransactionAction { implicit request =>
        val repo: PersonRepository = Neo4jServiceProviderImpl.get().neo4jD3Service.getPersonRepository()
        val dbPerson = repo.findOne(id)
        repo.delete(dbPerson)
        Redirect(routes.PersonController.personList)
    }

    def edit(id: Long) = Neo4jTransactionAction { implicit request =>
        val repo: PersonRepository = Neo4jServiceProviderImpl.get().neo4jD3Service.getPersonRepository()
        val dbPerson:Person = repo.findOne(id)
        val casePerson = CasePerson(dbPerson.getId(), dbPerson.getName(), dbPerson.getEmail(), dbPerson.getPassword(), dbPerson.getNickname())

        Neo4jServiceProviderImpl.get().template.save(dbPerson.familiarWith(dbPerson, 5, "Well known."))
        Ok(views.html.person.personEdit(id, personForm.fill(casePerson), "edit"))
    }

    def postCreate() = Neo4jTransactionAction { implicit request =>
        personForm.bindFromRequest().fold(formWithErrors => {
            Ok(views.html.person.personEdit(0L, formWithErrors, "create"))
        }, form => {
            val repo: PersonRepository = Neo4jServiceProviderImpl.get().neo4jD3Service.getPersonRepository()
            if (repo.findByEmail(form.email) == null) {
                val person = new Person(form.name, form.email, form.password, form.nickname)
                repo.save(person)
                Redirect(routes.PersonController.personList)
            } else {
                Ok(views.html.person.personEdit(0L, personForm.fill(form).withError("", "error.duplicateEmail"), "create"))
            }
        })
    }

    def postEdit(id: Long) = Neo4jTransactionAction { implicit request =>
        personForm.bindFromRequest().fold(formWithErrors => {
            Ok(views.html.person.personEdit(id, formWithErrors, "edit"))
        }, form => {
            val repo: PersonRepository = Neo4jServiceProviderImpl.get().neo4jD3Service.getPersonRepository()
            val dbPerson:Person = repo.findOne(id)
            dbPerson.setEmail(form.email)
            dbPerson.setName(form.name)
            if (StringUtils.isNotBlank(dbPerson.getPassword())) dbPerson.setPassword(form.password)
            dbPerson.setNickname(form.nickname)
//            if (repo.findByEmail(form.email) == null) {
                repo.save(dbPerson)
                Redirect(routes.PersonController.personList)
//            } else {
//                Ok(views.html.person.personEdit(id, personForm.fill(form).withError("", "error.notRight"), "edit"))
//            }
        })
    }
}
