from os import environ

import digitalocean

API_KEY = environ['ASL_DIGITAL_OCEAN_API_KEY']
CLIENT_ID = environ['ASL_DIGITAL_OCEAN_CLIENT_ID']

do = digitalocean.Manager(client_id=CLIENT_ID, api_key=API_KEY)


def getdroplets():
    return do.get_all_droplets()


def getclients():
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    """
    return [(droplet.ip_address, droplet.private_ip_address)
            for droplet in getdroplets() if 'client' in droplet.name]


def getservers():
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    """
    return [(droplet.ip_address, droplet.private_ip_address)
            for droplet in getdroplets() if 'server' in droplet.name]


def getdatabase():
    """ Get a list of 2-tuples where the first element is the droplet's global ip,
    and the second element is the droplet's local ip.
    It is assumed that there can be only one database.
    """
    return [(droplet.ip_address, droplet.private_ip_address)
            for droplet in getdroplets() if 'database' in droplet.name]


def createclient(size='512mb'):
    clientnumber = len(getclients()) + 1
    name = 'asl-client-{}'.format(clientnumber)
    _createdroplet(name=name, image_id=1001057, size=size)


def createserver(size='512mb'):
    servernumber = len(getservers()) + 1
    name = 'asl-server-{}'.format(servernumber)
    _createdroplet(name=name, image_id=1001057, size=size)


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
    droplet.create(ssh_key_ids=[41180, 46980, 46997], private_networking=True)


def destroyalldroplets():
    map(lambda droplet: droplet.destroy(), getdroplets())
