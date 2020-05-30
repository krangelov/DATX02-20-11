package org.grammaticalframework.grammarlex.gf

import org.grammaticalframework.pgf.Concr
import org.grammaticalframework.pgf.Expr

class Sentence(val syntaxTree: Expr, val lang: Concr) {

    val stringRep: String
        get() = GF.linearize(syntaxTree, lang)

    val listPhrases: List<String>
        get() = stringRep.split(" ")

    fun getListWords(): List<Word> {
        return TODO()
    }


}
