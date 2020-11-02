package ioanarotaru.kotlinproject.issues_comp.data

data class Issue(
    val id: String,
    var title: String,
    var description: String,
    var state: String
) {
    override fun toString(): String = title+"\n"+description+"\n"+state
}