import api, { unwrap } from './http'
import type { OntologyImportPayload, OntologyImportResult } from '../types'

export function getDefaultOntologySeed() {
  return unwrap<OntologyImportPayload>(api.get('/admin/ontology/default'))
}

export function importDefaultOntologySeed(dryRun = false) {
  return unwrap<OntologyImportResult>(api.post('/admin/ontology/import/default', null, { params: { dryRun } }))
}

export function importOntologySeed(payload: OntologyImportPayload, dryRun = false) {
  return unwrap<OntologyImportResult>(api.post('/admin/ontology/import', payload, { params: { dryRun } }))
}
