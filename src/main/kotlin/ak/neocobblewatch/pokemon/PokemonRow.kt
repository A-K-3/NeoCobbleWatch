package ak.neocobblewatch.pokemon

import ak.neocobblewatch.persistence.setBool
import ak.neocobblewatch.persistence.setJson
import ak.neocobblewatch.persistence.getBool
import ak.neocobblewatch.persistence.getJson
import java.sql.PreparedStatement
import java.sql.ResultSet

/** Shared by party_slots and pc_slots — caller binds its own PK columns + snapshot_at around this block. */
internal object PokemonRow {
    const val COLUMN_COUNT = 18

    const val COLUMNS = "pokemon_uuid, species_id, form_name, nickname, level, shiny, gender, " +
        "nature, ability, ball, tera_type, held_item, ot_name, friendship, ivs, evs, moves, aspects"

    const val PLACEHOLDERS = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"

    fun bind(stmt: PreparedStatement, startIndex: Int, p: PokemonSnapshot): Int {
        var i = startIndex
        stmt.setString(i++, p.pokemonUuid)
        stmt.setString(i++, p.speciesId)
        stmt.setString(i++, p.formName)
        stmt.setString(i++, p.nickname)
        stmt.setInt(i++, p.level)
        stmt.setBool(i++, p.shiny)
        stmt.setString(i++, p.gender)
        stmt.setString(i++, p.nature)
        stmt.setString(i++, p.ability)
        stmt.setString(i++, p.ball)
        stmt.setString(i++, p.teraType)
        stmt.setString(i++, p.heldItem)
        stmt.setString(i++, p.otName)
        stmt.setInt(i++, p.friendship)
        stmt.setJson(i++, p.ivs)
        stmt.setJson(i++, p.evs)
        stmt.setJson(i++, p.moves)
        stmt.setJson(i++, p.aspects)
        return i
    }

    fun read(rs: ResultSet): PokemonSnapshot = PokemonSnapshot(
        pokemonUuid = rs.getString("pokemon_uuid"),
        speciesId = rs.getString("species_id"),
        formName = rs.getString("form_name"),
        nickname = rs.getString("nickname"),
        level = rs.getInt("level"),
        shiny = rs.getBool("shiny"),
        gender = rs.getString("gender"),
        nature = rs.getString("nature"),
        ability = rs.getString("ability"),
        ball = rs.getString("ball"),
        teraType = rs.getString("tera_type"),
        heldItem = rs.getString("held_item"),
        otName = rs.getString("ot_name"),
        friendship = rs.getInt("friendship"),
        ivs = rs.getJson("ivs"),
        evs = rs.getJson("evs"),
        moves = rs.getJson("moves"),
        aspects = rs.getJson("aspects"),
    )
}
