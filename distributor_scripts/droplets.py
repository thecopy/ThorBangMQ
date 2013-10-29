from os import environ

import digitalocean

API_KEY = environ['ASL_DIGITAL_OCEAN_API_KEY']
CLIENT_ID = environ['ASL_DIGITAL_OCEAN_CLIENT_ID']

do = digitalocean.Manager(client_id=CLIENT_ID, api_key=API_KEY)

droplets = do.get_all_droplets()

CLIENT_NUMBER = 1
SERVER_NUMBER = 1


def main():
    global CLIENT_NUMBER, SERVER_NUMBER
    CLIENT_NUMBER = len(getclients()) + 1
    SERVER_NUMBER = len(getservers()) + 1


def getclients():
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    """
    clients = []
    for droplet in droplets:
        if 'client' in droplet.name:
            clients.append((droplet.ip_address, droplet.private_ip_address))
    return clients


def getservers():
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    """
    servers = []
    for droplet in droplets:
        if 'server' in droplet.name:
            servers.append((droplet.ip_address, droplet.private_ip_address))
    return servers


def getdatabase():
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    It is assumed that there can be only one database.
    """
    for droplet in droplets:
        if 'database' in droplet.name:
            return (droplet.ip_address, droplet.private_ip_address)
    return []


def createclient(size='512mb'):
    global CLIENT_NUMBER
    name = 'asl-client-{}'.format(CLIENT_NUMBER)
    _createdroplet(name=name, image_id=1001057, size=size)
    CLIENT_NUMBER += 1


def createserver(size='512mb'):
    global SERVER_NUMBER
    name = 'asl-server-{}'.format(SERVER_NUMBER)
    _createdroplet(name=name, image_id=1001057, size=size)
    SERVER_NUMBER += 1


def createdatabase(size='512mb'):
    _createdroplet(name='asl-database', image_id=949272, size=size)


def _createdroplet(name, image_id, size='512mb'):
    size = size.lower()
    if size == '512mb':
        size_id = 66
    elif size == '1gb':
        size_id = 63
    elif size == '2gb':
        size_id = 62
    elif size == '4gb':
        size_id = 64
    elif size == '8gb':
        size_id = 65
    else:
        size_id = 66

    droplet = digitalocean.Droplet(api_key=API_KEY,
                                   client_id=CLIENT_ID,
                                   name=name,
                                   region_id=4,
                                   image_id=image_id,
                                   size_id=size_id)
    droplet.create(ssh_key_ids=[41180, 41181], private_networking=True)


def destroyalldroplets():
    for droplet in droplets:
        droplet.destroy()

if __name__ == '__main__':
    main()
