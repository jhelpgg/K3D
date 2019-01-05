package khelp.ui.textEditor.decoration

import khelp.math.Rational

enum class TextSize
{
    SMALL(Rational.createRational(3, 4)),
    NORMAL(Rational.ONE),
    BIG(Rational.createRational(5, 4));

    private val factor: Rational

    constructor(factor: Rational)
    {
        this.factor = factor
    }

    fun computeSize(normalTextSize: Int) =
            ((normalTextSize * this.factor.numerator()) / this.factor.denominator()).toInt()
}