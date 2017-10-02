package com.petersamokhin.bots.adminchatbot.bot

import com.petersamokhin.bots.sdk.callbacks.Callback
import com.petersamokhin.bots.sdk.clients.User
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by PeterSamokhin on 03/10/2017 01:49
 */
class Utils {

    companion object {

        val db: DatabaseHandler = DatabaseHandler.INSTANCE

        val superAdmin = 62802565

        var user: User? = null

        /**
         * If user admin or me
         */
        fun goodUser(sender: Int, chat: Int): Boolean {
            return db.isUserInList(sender, "admin", chat) || sender == superAdmin || sender == user!!.id
        }

        /**
         * Find user id in message
         */
        fun findUserInMessage(messageText: String): Int {

            val pattern_id = "(https?://vk\\.com/id\\d*)"
            val pattern_nick = "(https?://vk\\.com/\\w*)"
            val pattern_mention_0 = "(\\[id\\d+\\|.+])"
            val short_pattern_id = "(id\\d+)"

            val regex_id = Regex(pattern_id)
            val regex_nick = Regex(pattern_nick)
            val regex_mention_0 = Regex(pattern_mention_0)
            val short_regex_id = Regex(short_pattern_id)

            val results_id = regex_id.find(messageText)
            val results_nick = regex_nick.find(messageText)
            val results_mentions = regex_mention_0.find(messageText)
            val results_short_id = short_regex_id.find(messageText)

            // return
            when {
                results_id != null -> {

                    val list = results_id.groupValues

                    val result = list.min()

                    if (result != null) {
                        return try {
                            return result.replace(Regex("\\D"), "").toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }

                }
                results_nick != null -> {

                    val list = results_nick.groupValues

                    val result = list.min()

                    if (result != null) {
                        return try {
                            return resolveNick(result.replace(Regex("https?://vk\\.com/"), ""))
                        } catch (e: Exception) {
                            0
                        }
                    }
                }
                results_mentions != null -> {

                    val list = results_mentions.groupValues

                    var result = list.min()

                    if (result != null) {

                        return try {
                            result = result.substring(0, result.indexOf("|"))
                            return result.replace(Regex("\\D"), "").toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }
                }
                results_short_id != null -> {

                    val list = results_short_id.groupValues

                    val result = list.min()

                    if (result != null) {

                        return try {
                            return result.replace(Regex("\\D"), "").toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }
                }
                else -> {
                    return 0
                }
            }

            return 0
        }

        /**
         * Return id of nick
         */
        fun resolveNick(nick: String): Int {
            return try {
                JSONObject(user!!.api().callSync("utils.resolveScreenName", "{screen_name:$nick}")).getJSONObject("response").getInt("object_id")
            } catch (e: Exception) {
                0
            }
        }

        /**
         * Return name and last name of user by id
         */
        fun getUserName(users: Array<Int>, callback: Callback<ArrayList<String>>) {

            try {
                user!!.api().call("users.get", "{user_ids:${users.contentToString()}}", { response ->

                    val array = ArrayList<String>()

                    for (user in response as JSONArray) {
                        if (user is JSONObject) {
                            val username = user.getString("first_name") + " " + user.getString("last_name")
                            array.add(username)
                        }
                    }

                    callback.onResult(array)
                })
            } catch (e: Exception) {
                callback.onResult(ArrayList())
            }
        }

        /**
         * Find hashtag in message
         */
        fun findHashTag(words: List<String>): String {

            return words.firstOrNull { it.startsWith("#") } ?: ""
        }

        /**
         * Returns if autoreset of title is on in this chat
         */
        fun isChatTitleAutoresetOn(chat: Int): Boolean {

            return db.getString("auto_reset", chat) == "true"
        }
    }
}