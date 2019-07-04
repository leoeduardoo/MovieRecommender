var rdf = require('rdf-ext')
var util = require('util')
var AbstractSerializer = require('rdf-serializer-abstract')

function SparqlUpdateSerializer () {
  AbstractSerializer.call(this, rdf)
}

util.inherits(SparqlUpdateSerializer, AbstractSerializer)

SparqlUpdateSerializer.prototype.serialize = function (graph, done) {
  return new Promise(function (resolve) {
    done = done || function () {}

    var nTriples = 'INSERT DATA { '

    graph.forEach(function (triple) {
      nTriples += triple.toString() + '\n'
    })

    nTriples += ' }'

    done(null, nTriples)
    resolve(nTriples)
  })
}

// add singleton methods to class
var instance = new SparqlUpdateSerializer()

for (var property in instance) {
  SparqlUpdateSerializer[property] = instance[property]
}

module.exports = SparqlUpdateSerializer
