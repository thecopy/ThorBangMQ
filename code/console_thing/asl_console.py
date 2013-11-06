from functools import partial
import psycopg2

conn = psycopg2.connect(host="localhost", database="asl", user="asl", password="asl2013")
cur = conn.cursor()

OPTIONS = ()  # this is defined at bottom of file


def main():
    while True:
        printoptions()
        try:
            input_ = raw_input().split(' ')
            choice = int(input_[0])
            OPTIONS[choice]  # check if input_ is > len(OPTIONS)
        except KeyboardInterrupt:
            print "\nProgram stopped"
            break
        except Exception, e:
            print e
            print "You must choose a number between 0 and {}".format(len(OPTIONS) - 1)
            continue
        if len(input_) > 1:
            args = input_[1:]
        else:
            args = []

        parse_perform_command(cur, choice, args)


def printoptions():
    print "Please choose an option:"
    for i, (option, __) in enumerate(OPTIONS):
        print "{id}: {description}".format(id=i, description=option)


def parse_perform_command(cur, choice, args):
    assert(choice <= len(OPTIONS))
    option, fun = OPTIONS[choice]
    try:
        res = fun(cur, *args)
    except TypeError:
        res = "Wrong number of arguments!"
    print "---------"
    print "{}: {}".format(option, res)
    print "---------"


def clientcount(cur):
    return _count(cur, 'clients')


def messagecount(cur):
    return _count(cur, 'messages')


def queuecount(cur):
    return _count(cur, 'queues')


def _count(cur, table):
    cur.execute("SELECT COUNT(*) FROM {table};".format(table=table))
    res = cur.fetchone()
    if res is None:
        return 0
    return res[0]


def firstxmessages(cur, num_msgs):
    try:
        num_msgs = int(num_msgs)
    except Exception:
        print "I take an integer as an argument!"
        return
    messagestr = ("{id}\t{sender_id}\t{receiver_id}\t{queue_id}\t{time_of_arrival}\t"
                  "{priority}\t{context_id}\t'{message}'")
    ret = ["\nid\tsnd_id\trecv_id\tqueue_id\ttoa\t\t\tprio\tcntx_id\tmsg"]
    cur.execute("SELECT * FROM messages LIMIT {count};".format(count=num_msgs))
    for i in range(num_msgs):
        res = cur.fetchone()
        if res is None:
            print "No more results"
            break
        (id_, sender_id, receiver_id, queue_id,
         time_of_arrival, priority, context_id, message) = res
        ret.append(messagestr.format(id=id_, sender_id=sender_id, receiver_id=receiver_id,
                                        queue_id=queue_id, time_of_arrival=time_of_arrival,
                                        priority=priority, context_id=context_id, message=message))
    return "\n".join(ret)

OPTIONS = (
    ("Number of messages", messagecount),
    ("Number of queues", queuecount),
    ("Number of clients", clientcount),
    ("First messages <num messages>", firstxmessages),
)

if __name__ == '__main__':
    main()
