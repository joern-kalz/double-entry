import connexion
from flask_cors import CORS

app = connexion.App(__name__, specification_dir = './')
app.add_api('openapi.yml')
CORS(app.app)

if __name__ == '__main__':
    app.run(debug = True)