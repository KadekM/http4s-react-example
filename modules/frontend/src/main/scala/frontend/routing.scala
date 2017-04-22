package frontend

object routing {
  sealed trait AppPage

  case object Home extends AppPage
  case object Heroes extends AppPage
  //final case class HeroDetail(heroId: H

}
