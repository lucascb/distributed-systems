
service Server  {
    string GET(1:string path),
    string LIST(1: string path),
    bool ADD(1: string path, 2: string data),
    bool UPDATE(1: string path, 2: string data),
    bool DELETE(1: string path),
    bool UPDATE_VERSION(1: string path, 2: string data, 3: i32 version),
    bool DELETE_VERSION(1: string path, 2: i32 version)
}
