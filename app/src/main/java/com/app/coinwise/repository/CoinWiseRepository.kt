package com.app.coinwise.repository

import com.app.coinwise.data.local.Dao
import com.app.coinwise.data.local.Table


class CoinWiseRepository (private val dao: Dao) {

    suspend fun saveList (table: List<ListDTO>?){
        dao.insert(table)

    }

    suspend fun getList(): List<Table>{

        return dao.getAll()

    }

}