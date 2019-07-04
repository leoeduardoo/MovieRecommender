/* global describe, it */
var assert = require('assert')
var rdf = require('rdf-ext')
var SparqlUpdateSerializer = require('../')

var simpleGraph = rdf.createGraph()

simpleGraph.add(rdf.createTriple(
  rdf.createNamedNode('http://example.org/subject'),
  rdf.createNamedNode('http://example.org/predicate'),
  rdf.createLiteral('object')
))

var simpleUpdate = 'INSERT DATA { <http://example.org/subject> <http://example.org/predicate> "object" .\n }'

describe('SPARQL Update serializer', function () {
  describe('instance API', function () {
    describe('callback API', function () {
      it('should be supported', function (done) {
        var serializer = new SparqlUpdateSerializer()

        Promise.resolve(new Promise(function (resolve, reject) {
          serializer.serialize(simpleGraph, function (error, nTriples) {
            if (error) {
              reject(error)
            } else {
              resolve(nTriples)
            }
          })
        })).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleUpdate)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Promise API', function () {
      it('should be supported', function (done) {
        var serializer = new SparqlUpdateSerializer()

        serializer.serialize(simpleGraph).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleUpdate)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Stream API', function () {
      it('should be supported', function (done) {
        var serializer = new SparqlUpdateSerializer()
        var nTriples

        serializer.stream(simpleGraph).on('data', function (data) {
          nTriples = data
        }).on('end', function () {
          if (!nTriples) {
            done('no data streamed')
          } else if (nTriples.trim() !== simpleUpdate) {
            done('wrong output')
          } else {
            done()
          }
        }).on('error', function (error) {
          done(error)
        })
      })
    })
  })

  describe('static API', function () {
    describe('callback API', function () {
      it('should be supported', function (done) {
        Promise.resolve(new Promise(function (resolve, reject) {
          SparqlUpdateSerializer.serialize(simpleGraph, function (error, nTriples) {
            if (error) {
              reject(error)
            } else {
              resolve(nTriples)
            }
          })
        })).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleUpdate)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Promise API', function () {
      it('should be supported', function (done) {
        SparqlUpdateSerializer.serialize(simpleGraph).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleUpdate)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Stream API', function () {
      it('should be supported', function (done) {
        var nTriples

        SparqlUpdateSerializer.stream(simpleGraph).on('data', function (data) {
          nTriples = data
        }).on('end', function () {
          if (!nTriples) {
            done('no data streamed')
          } else if (nTriples.trim() !== simpleUpdate) {
            done('wrong output')
          } else {
            done()
          }
        }).on('error', function (error) {
          done(error)
        })
      })
    })
  })
})
