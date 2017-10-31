module generated {
    sequence<string> StringSeq;

    interface RegistrableI {
        bool registerSeeder(string fileHash);
        bool deregisterSeeder(string fileHash);
        bool sendHashes(StringSeq chunkHashes, StringSeq chunkIDs, string fileHash, string seederIP, int seederPort);
        bool initializeDB(string fileHash, string filepath, int fileSize, int videoSizeX, int videoSizeY, int bitrate);

    };
};