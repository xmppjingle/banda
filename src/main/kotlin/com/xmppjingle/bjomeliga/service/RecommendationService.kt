package com.xmppjingle.bjomeliga.service

import com.redislabs.redisgraph.impl.api.RedisGraph
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class RecommendationService {

    lateinit var graphClient: RedisGraph
    private val graphId: String = "default"

    @PostConstruct
    fun init() {
        graphClient = RedisGraph("localhost", 6333)
    }

    fun getLikelyChoice(choiceId: String, tags: Set<String>): Map<String, Double> {
        val query = """
        MATCH (c:Choice {id: '$choiceId'})-[:HAS_OPTION]->(o:Option)<-[:SELECTED]-(u:User)-[:HAS_TAG]->(t:Tag)
        WHERE t.id IN [${tags.joinToString(",") { "'$it'" }}]
        WITH o, COUNT(*) AS score
        RETURN o.id AS option, score AS likelihood
        ORDER BY score DESC
    """
        val result = graphClient.query(graphId, query)
        return result.map { it.getValue("option") as String to it.getValue("likelihood") as Double }.toMap()
    }


    fun addChoiceEvent(choiceId: String, userId: String, selectedOption: String, options: Set<String>) {
        // Create nodes and relationships in Redis Graph
        graphClient.query(graphId,
            "CREATE (c|Choice {id: '$choiceId'})-[|HAS_OPTION]->(o|Option {id: '$selectedOption'}) " +
                    "CREATE (u|User {id: '$userId'})-[|MADE_CHOICE]->(c) " +
                    "CREATE (u)-[|SELECTED]->(o) " +
                    options.forEach { option ->
                        graphClient.query(graphId, "CREATE (c)-[|HAS_OPTION]->(|Option {id: '$option'})")
                    }
        )
    }

    fun addUserTags(userId: String, tags: Set<String>) {
        // Create nodes and relationships in Redis Graph
        tags.forEach{
            graphClient.query(graphId,
                "MERGE (u:User {id: '$userId'})-[:HAS_TAG]->(t:Tag {id: '$it'})"
            )
        }
    }

    /* Add choice event to Redis Graph database */
    fun addChoiceEventToRedisGraph(choiceId: String, userId: String, selectedOption: String, options: Set<String>){
        // Create choice node if it doesn't exist
        graphClient.query(
            graphId, "MERGE (c:Choice {id: '$choiceId'})"
        )

        // Create option nodes if they don't exist
        options.forEach {
            graphClient.query(
                graphId, "MERGE (o:Option {id: '$it'})"
            )
        }

        // Create user node if it doesn't exist
        graphClient.query(
            graphId, "MERGE (u:User {id: '$userId'})"
        )

        // Create choice-option relationship if it doesn't exist
        graphClient.query(
            graphId, "MATCH (c:Choice {id: '$choiceId'}) " +
                    "MATCH (o:Option {id: '$selectedOption'}) " +
                    "MERGE (c)-[:HAS_OPTION]->(o)"
        )

        // Create user-choice relationship if it doesn't exist
        graphClient.query(
            graphId, "MATCH (c:Choice {id: '$choiceId'}) " +
                    "MATCH (u:User {id: '$userId'}) " +
                    "MERGE (u)-[:MADE_CHOICE]->(c)"
        )

        // Create user-option relationship if it doesn't exist
        graphClient.query(
            graphId, "MATCH (o:Option {id: '$selectedOption'}) " +
                    "MATCH (u:User {id: '$userId'}) " +
                    "MERGE (u)-[:WAS_SELECTED]->(o)"
        )
    }

//    fun addChoiceEvent(choiceId: String, userId: String, selectedOption: String, options: Set<String>) {
//        // Create nodes and relationships in Redis Graph
//        var query = "CREATE (c:Choice {id: $choiceId}), (o:Option {id: $selectedOption}), (u:User {id: $userId})"
////        CREATE (c:Choice {id: '$choiceId'})-[:HAS_OPTION]->(o:Option {id: '$selectedOption'}) """
//////        CREATE (u:User {id: $userId})-[:MADE_CHOICE]->(c)
////        CREATE (u)-[:SELECTED]->(o)
////    """
////        options.forEach { option ->
////            query += """
////            CREATE (c)-[:HAS_OPTION]->(:Option {id: $option})
////        """
////        }
//        println(query)
//        graphClient.query(graphId, query)
//    }

}
