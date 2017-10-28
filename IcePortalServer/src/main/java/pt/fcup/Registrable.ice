module generated {
    sequence<string> StringSeq;

    interface RegistrableI {
        bool registerSeeder(string fileHash, string fileName, int fileSize, string protocol, int port,
                            int videoSizeX, int videoSizeY, int bitrate);
        bool deregisterSeeder(string deregMessage);
        bool sendHashes(StringSeq chunkHashes, string fileHash, string seederIP, int seederPort);

    };
};