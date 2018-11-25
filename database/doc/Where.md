# Where clause
   1. [Column equals a value](Where.md#column-equals-a-value)
   1. [Column inside a set of values](Where.md#column-inside-a-set-of-values)
   1. [Column match a select request](Where.md#column-match-a-select-request)
   1. [Create condition for column match a regular expression](Where.md#create-condition-for-column-match-a-regular-expression)
   1. [Conditions combination](Where.md#conditions-combination)
      1. [**not()**: Negate a condition](Where.md#not():-negate-a-condition)
      1. [**AND**: Match two conditions](Where.md#and:-match-two-conditions)
      1. [**OR**: Match at least one condition](Where.md#or:-match-at-least-one-condition)

Where clause is way to specify condition in row selection, for show, delete, modify data.

### Column equals a value

To select when a column have an exact value, use [khelp.database.condition.ConditionColumnEquals](../src/khelp/database/condition/ConditionColumnEquals.kt)

It is possible to use the object:

````Kotlin
ConditionColumnEquals("name", "Joe")
````

Or the infix String extension **EQUALS":

````Kotlin
"name" EQUALS "Joe"
````

Example:

````Kotlin
val result = database.select(SelectQuery("Person", arrayOf("name")) WHERE ("address" EQUALS "221B Baker Street"))
````

### Column inside a set of values

To select column with a set of possible value, use [khelp.database.condition.ConditionColumnOneOf](../src/khelp/database/condition/ConditionColumnOneOf.kt)

It is possible to use the object:

````Kotlin
ConditionColumnOneOf("age", intArrayOf(42, 73, 21, 12, 37))
````

Or the String extension **oneOf**

````Kotlin
"age".oneOf(intArrayOf(42, 73, 21, 12, 37))
````

Example:

````Kotlin
database.delete(DeleteQuery("Person", "age".oneOf(intArrayOf(42, 73, 21, 12, 37))))
````

### Column match a select request

Imagine have two table:

     PERSON
 
     | Name | Age |
     |------+-----|
     | Joe  | 42  |
     ......
     
     PROMOTION
     
     | Reduction | Age |
     |-----------+-----|
     |  25       | 20  |
     ....
     
Now for get all persons name that can have a specific reduction, it have to: 
- Select all age corresponds to the reduction

````Kotlin
val agesOfReductionQuery = SelectQuery("Reduction", arrayOf("age")) WHERE ("Reduction" EQUALS reduction)
````

- For each result element select persons name with good age

For the last point the first idea can be do a loop on **agesOfReductionQuery** results and cumulate the results.
It works, byt not very efficient

To speed up things the second idea is to use the **ConditionColumnOneOf** on collect age in array.
It is better than previous idea, but we can do even better.

The best choice is to use [khelp.database.condition.ConditionColumnMatchSelect](../src/khelp/database/condition/ConditionColumnMatchSelect.kt)
or the corresponding String infix extension **MATCH**

The final condition become:

````Kotlin
"age" MATCH agesOfReductionQuery
````

Example :

````Kotlin
val agesOfReductionQuery = SelectQuery("Reduction", arrayOf("age")) WHERE ("Reduction" EQUALS reduction)
val result = database.select(SelectQuery("Person", arrayOf("name")) WHERE ("age" MATCH agesOfReductionQuery))
````

Its very power full, because it use the power of the database. Things can be more complex, let imagination play.

### Create condition for column match a regular expression

Sometimes we need that a column match a regular expression. 
For this, create condition with **conditionRegex** method of **Database**:

````Kotlin
val condition = database.conditionRegex("Person", "name", 'T'.regex() + ANY.zeroOrMore())
````

The example match any person with name start by **'T'**

It can be use in any where clause, by example:

````Kotlin
val condition = database.conditionRegex("Person", "name", 'T'.regex() + ANY.zeroOrMore())
val result3 = database.stringIteratorFromColumn("Person", "name", condition)
result3.forEach { debug("Regex : person = ", it) }
````

### Conditions combination

It can be use full to combine where clauses, by example have person with name start with **'T'** and age is **42**.

##### not(): Negate a condition

For take the opposite of condition, use the **ConditionNot** object or **not()** method extension.

Example for all person not named **"Joe"**:

````Kotlin
("name" EQUALS "Joe").not()
````

##### AND: Match two conditions

For match 2 conditions in same time, use **ConditionAnd** object or the infix extension **AND**

Example person with name start with **'T'** and age is **42** :

````Kotlin
database.conditionRegex("Person", "name", 'T'.regex() + ANY.zeroOrMore()) AND ("age" EQUALS 42)
````

##### OR: Match at least one condition

For match one condition or one other condition, use **ConditionOr** object or the infix extension **OR**

Example person named **"Joe"** or age is **42** :

````Kotlin
("name" EQUALS "Joe") OR ("age" EQUALS 42) 
````

[Back to menu](Menu.md#menu)
