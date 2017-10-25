module generated {
    sequence<string> StringSeq;

    interface RegistrableI {
        bool registerSeeder(string regMessage);
        bool deregisterSeeder(string deregMessage);
        bool sendHashes(StringSeq chunkHashes, string fileHash, string seederIP, string seederPort);

    };
};