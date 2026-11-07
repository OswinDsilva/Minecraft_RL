import socket
import struct

s = socket.socket()

s.connect(("127.0.0.1", 8000))

actions = ("UP", "DOWN", "LEFT", "RIGHT", "OVER")

while True:
    message = input("Action:")
    if message.upper() not in actions:
        print("Invalid")
        continue
    if message == "Over":
        break
    s.sendall((message+"\n").encode())
    
    length_data = s.recv(4)
    length = struct.unpack("!I", length_data)[0]

    recieved = 0
    fragments = []
    while(recieved < length):
        data = s.recv(min(4096, length - recieved))

        fragments.append(data)
        recieved += len(data)

    msg = b"".join(fragments)

    print(msg.decode("utf-8"))

s.close()