package mk.ukim.finki.mpip.gameslibrary.Models

data class Game(
    var name: String = "",
    var genre: String = "",
    var year: String = "",
    var platforms: String = "",
    var imageUrl: String = "",
    var isInLibrary: Boolean = false
) {
    constructor() : this("", "", "", "", "")
}
