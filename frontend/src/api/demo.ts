import api, { unwrap } from './http'
import type { DemoWorkspaceStatus } from '../types'

export function getDemoStatus() {
  return unwrap<DemoWorkspaceStatus>(api.get('/demo/status'))
}

export function startDemoWorkspace() {
  return unwrap<DemoWorkspaceStatus>(api.post('/demo/start'))
}

export function resetDemoWorkspace() {
  return unwrap<DemoWorkspaceStatus>(api.post('/demo/reset'))
}

export function deleteDemoWorkspace() {
  return unwrap<void>(api.delete('/demo'))
}
