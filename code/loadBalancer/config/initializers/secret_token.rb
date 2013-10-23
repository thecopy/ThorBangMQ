# Be sure to restart your server when you modify this file.

# Your secret key is used for verifying the integrity of signed cookies.
# If you change this key, all old signed cookies will become invalid!

# Make sure the secret is at least 30 characters and all random,
# no regular words or you'll be exposed to dictionary attacks.
# You can use `rake secret` to generate a secure secret key.

# Make sure your secret_key_base is kept private
# if you're sharing your code publicly.
LoadBalancer::Application.config.secret_key_base = 'de1a3a054f3aefca86da2d9b4cffc369484bae39cd15bb808ed5a10ffcd376adf46b1b60bd0ce9875775e849d36b2c52597c2407f33d1cb029ffe93dfcbbc6a8'
