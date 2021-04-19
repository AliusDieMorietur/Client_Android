package com.samurainomichi.cloud_storage_client.network

object Connection {
        private lateinit var INSTANCE: DataSource

        fun getInstance(dataSource: DataSource? = null): DataSource {
            if (!::INSTANCE.isInitialized) {
                if (dataSource != null)
                    INSTANCE = dataSource
                else
                    throw Exception("Data source must be presented on the first call of getInstance")
            }

            return INSTANCE
        }
}