from os import environ

import digitalocean

API_KEY = environ['ASL_DIGITAL_OCEAN_API_KEY']
CLIENT_ID = environ['ASL_DIGITAL_OCEAN_CLIENT_ID']

do = digitalocean.Manager(client_id=CLIENT_ID, api_key=API_KEY)

DEFAULT_DROPLET_SIZE = '4gb'


def getdroplets():
    return do.get_all_droplets()


def getnewtestid():
    ids = map(_getid, getdroplets())
    if not ids:
        ids.append(0)
    return max(ids) + 1


def getclients(testid):
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    """
    return [(droplet.ip_address, droplet.private_ip_address)
            for droplet in getdroplets() if 'client' in droplet.name and _getid(droplet) == testid]


def getservers(testid):
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    """
    return [(droplet.ip_address, droplet.private_ip_address)
            for droplet in getdroplets() if 'server' in droplet.name and _getid(droplet) == testid]


def getdatabase(testid):
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    It is assumed that there can be only one database.
    """
    return [(droplet.ip_address, droplet.private_ip_address)
            for droplet in getdroplets() if 'database' in droplet.name and _getid(droplet) == testid]


def createclient(size=DEFAULT_DROPLET_SIZE, testid=1):
    clientnumber = len(getclients(testid)) + 1
    name = 'asl-client-{}-id-{}'.format(clientnumber, testid)
    _createdroplet(name=name, image_id=1001057, size=size)


def createserver(size=DEFAULT_DROPLET_SIZE, testid=1):
    servernumber = len(getservers(testid)) + 1
    name = 'asl-server-{}-id-{}'.format(servernumber, testid)
    _createdroplet(name=name, image_id=1001057, size=size)


def createdatabase(size=DEFAULT_DROPLET_SIZE,  testid=1):
    _createdroplet(name='asl-database-id-{}'.format(testid),
                   image_id=1131016, size=size)


def _createdroplet(name, image_id, size):
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

    sshkeys = [41181, 46980, 46997]
    droplet = digitalocean.Droplet(api_key=API_KEY,
                                   client_id=CLIENT_ID,
                                   name=name,
                                   region_id=4,
                                   image_id=image_id,
                                   size_id=size_id,
                                   ssh_key_ids=sshkeys)
    droplet.create(ssh_key_ids=sshkeys, private_networking=True)


def destroyalldroplets(testid):
    for droplet in getdroplets():
        if _getid(droplet) == testid:
            droplet.destroy()


def _getid(droplet):
    try:
        return droplet.name.split('-')[-1]
    except Exception, e:
        print e
        return "-1"
