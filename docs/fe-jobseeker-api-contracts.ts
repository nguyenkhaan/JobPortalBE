export interface Job {
  id: string;
  title: string;
  companyName: string;
  type: string;
  isFeatured: boolean;
  logo: string;
  location: string;
  salary: string;
  daysRemaining: string;
  experience?: string;
  education?: string;
  jobLevel?: string;
}

export interface JobOverview {
  postedDate: string;
  expireIn: string;
  education: string;
  salary: string;
  location: string;
  jobType: string;
  experience: string;
}

export interface CompanyProfile {
  industry: string;
  foundedIn: string;
  orgType: string;
  companySize: string;
}

export interface JobDetailType {
  id: string;
  title: string;
  companyName: string;
  logo: string;
  type: string;
  isFeatured: boolean;
  website: string;
  phone: string;
  email: string;
  expireDate: string;
  description: string[];
  responsibilities: string[];
  overview: JobOverview;
  companyProfile: CompanyProfile;
}

export interface Employer {
  id: string;
  name: string;
  logo: string;
  location: string;
  openJobsCount: number;
  category?: string;
}

export interface EmployerOverview {
  founded: string;
  orgType: string;
  teamSize: string;
  industry: string;
}

export interface EmployerContact {
  website: string;
  phone: string;
  email: string;
}

export interface EmployerDetail {
  id: string;
  name: string;
  logo: string;
  category: string;
  description: string;
  benefits: string[];
  vision: string;
  overview: EmployerOverview;
  contact: EmployerContact;
}

export interface JobFilterParams {
  keyword?: string;
  location?: string;
  category?: string;
  jobType?: string;
  salaryMin?: number;
  salaryMax?: number;
  experience?: string;
  salaryRange?: string;
  jobTypes?: string[];
  education?: string[];
  jobLevel?: string;
  page: number;
  limit: number;
}

export interface EmployerFilterParams {
  keyword?: string;
  location?: string;
  category?: string;
  page: number;
  limit: number;
}

export interface ApplyJobRequest {
  jobId: string;
  resumeId: string;
  coverLetter: string;
}

// Types cho Dashboard
export interface AppliedJobType {
  id: string;
  logo: string;
  role: string;
  type: string;
  location: string;
  salary: string;
  dateApplied: string;
  status: string;
}

export interface FavoriteJobType {
  id: string;
  logo: string;
  role: string;
  type: string;
  location: string;
  salary: string;
  timeStatus: string;
  isExpired?: boolean;
}

export interface JobAlertItemType {
  id: string;
  logo: string;
  role: string;
  type: string;
  location: string;
  salary: string;
  daysRemaining: string;
}

export interface DashboardOverviewType {
  appliedCount: number;
  favoriteCount: number;
  alertCount: number;
  recentApplied: AppliedJobType[];
  isProfileCompleted: boolean;
}

export interface Resume {
    id: string;
    name: string; // Tên file hoặc tên CV
    fileUrl?: string; // Đường dẫn tải/xem CV (nếu có)
    createdAt?: string; // Ngày tạo (nếu có)
}