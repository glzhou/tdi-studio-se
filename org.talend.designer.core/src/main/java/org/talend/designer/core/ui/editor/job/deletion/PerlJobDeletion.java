// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.core.ui.editor.job.deletion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Delete jobs after Talend eidtor closed.
 * 
 * yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: PerlJobDeletion.java 下午04:22:37 2007-5-28 +0000 (2007-5-28) yzhang $
 * 
 */
public class PerlJobDeletion extends AbstractJobDeletion implements IJobDeletion {

    private static Map<Process, List<IResource>> jobFolders = Collections
            .synchronizedMap(new HashMap<Process, List<IResource>>());

    /**
     * yzhang PerlJobDeletion constructor comment.
     * 
     * @param process
     */
    public PerlJobDeletion(Process process) {
        super(process);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.job.deletion.IJobDeletion#storeResource(org.eclipse.core.resources.IResourceDelta)
     */
    public void storeResource(IResourceDelta root, Process p) {
        this.process = p;

        if (root == null && root.getAffectedChildren().length == 0) {
            return;
        }

        IResourceDelta[] childDeltas = root.getAffectedChildren();
        for (int i = 0; i < childDeltas.length; i++) {

            storeResource(childDeltas[i], p);
            IResourceDelta rd = childDeltas[i];

            if ((rd.getKind() == IResourceDelta.ADDED) && rd.getResource().getType() == IResource.FILE
                    && "pl".equalsIgnoreCase(rd.getResource().getFileExtension())) {
                String[] jobName = containRunJob(this.process);
                if (jobName != null) {
                    for (int j = 0; j < jobName.length; j++) {
                        if (!jobNamesWithInRunJob.contains(jobName[j])) {
                            jobNamesWithInRunJob.add(jobName[j]);
                        }
                    }
                }
                if (!jobFolders.containsKey(this.process)) {
                    List list = new ArrayList<IResource>();
                    list.add(rd.getResource());
                    jobFolders.put(this.process, list);
                } else {
                    jobFolders.get(this.process).add(rd.getResource());
                }

            }

        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.job.deletion.IJobDeletion#deleteJobs()
     */
    public void deleteJobs() {
        try {
            String[] jobName = containRunJob(this.process);
            if (jobName != null) {
                List<Process> jobs = new ArrayList<Process>();
                for (int i = 0; i < jobName.length; i++) {
                    for (Process storedProcess : jobFolders.keySet()) {
                        if (storedProcess != null && storedProcess.getName().equals(jobName[i])
                                && storedProcess.isClosed()) {
                            for (IResource perlFile : jobFolders.get(storedProcess)) {
                                perlFile.delete(true, null);
                            }
                            jobs.add(storedProcess);
                        }
                    }
                    jobNamesWithInRunJob.remove(jobName[i]);
                }
                if (jobs.size() > 0) {
                    for (Process job : jobs) {
                        jobFolders.remove(job);
                    }
                }
                for (IResource perlFile : jobFolders.get(this.process)) {
                    String[] arg1 = perlFile.getName().split("\\.");
                    String[] arg2 = arg1[1].split("_");
                    Process p = TalendEditor.isProcessOpend(arg2[1]);

                    if (p != null) {
                        List<IResource> list = jobFolders.get(p);
                        if (list == null) {
                            List<IResource> d = new ArrayList<IResource>();
                            d.add(perlFile);
                            jobFolders.put(p, d);
                        } else {
                            list.add(perlFile);
                        }
                        continue;
                    }
                    perlFile.delete(true, null);
                }
                jobFolders.remove(this.process);

            } else if (!withinRunJob(this.process)) {
                for (IResource perlFile : jobFolders.get(this.process)) {
                    perlFile.delete(true, null);
                }
                jobFolders.remove(this.process);
            }

        } catch (CoreException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.job.deletion.IJobDeletion#deleteRelatedJobs(org.eclipse.core.resources.IFile)
     */
    public void deleteRelatedJobs(IFile file) throws CoreException {
        // find the context scripts related to the job file fisrt, then remove them all.
        IResource[] resources = file.getParent().members();
        String jobFileName = file.getName().replaceAll("." + file.getFileExtension(), "");

        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            if (resource.getName().matches(jobFileName + "_(.)+\\.pl")) {
                resource.delete(true, null);
            }
        }
        file.delete(true, null);
    }
}
